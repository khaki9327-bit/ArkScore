package com.arkscore.solana;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.arkscore.walletdata.WalletData;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class SolanaRpcClientTest {

    private MockRestServiceServer server;
    private SolanaRpcProperties properties;
    private SolanaRpcClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://solana.test");

        server = MockRestServiceServer.bindTo(builder).build();
        properties = new SolanaRpcProperties();
        properties.setRequestMinInterval(Duration.ZERO);
        properties.setRateLimitRetryBackoff(Duration.ZERO);
        client = new SolanaRpcClient(builder.build(), properties);
    }

    @Test
    void fetchesBalanceLamports() {
        server.expect(requestTo("https://solana.test/"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("""
                        {
                          "jsonrpc": "2.0",
                          "method": "getBalance",
                          "params": [
                            "wallet123",
                            {
                              "commitment": "finalized"
                            }
                          ]
                        }
                        """))
                .andRespond(withSuccess("""
                        {
                          "jsonrpc": "2.0",
                          "result": {
                            "value": 1230000000
                          },
                          "id": 1
                        }
                        """, MediaType.APPLICATION_JSON));

        assertThat(client.getBalanceLamports("wallet123")).isEqualTo(1_230_000_000L);
        server.verify();
    }

    @Test
    void fetchesTokenAccountsByOwner() {
        server.expect(requestTo("https://solana.test/"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("""
                        {
                          "jsonrpc": "2.0",
                          "method": "getTokenAccountsByOwner",
                          "params": [
                            "wallet123",
                            {
                              "programId": "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"
                            },
                            {
                              "encoding": "jsonParsed",
                              "commitment": "finalized"
                            }
                          ]
                        }
                        """))
                .andRespond(withSuccess("""
                        {
                          "jsonrpc": "2.0",
                          "result": {
                            "value": [
                              {
                                "pubkey": "tokenAccount1",
                                "account": {
                                  "data": {
                                    "parsed": {
                                      "info": {
                                        "mint": "MintA",
                                        "tokenAmount": {
                                          "amount": "100",
                                          "decimals": 6
                                        }
                                      }
                                    }
                                  }
                                }
                              }
                            ]
                          },
                          "id": 1
                        }
                        """, MediaType.APPLICATION_JSON));

        assertThat(client.getTokenAccountsByOwner("wallet123"))
                .containsExactly(new WalletData.TokenHolding("tokenAccount1", "MintA", "100", 6));
        server.verify();
    }

    @Test
    void fetchesSignaturesWithConfiguredLimit() {
        server.expect(requestTo("https://solana.test/"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("""
                        {
                          "jsonrpc": "2.0",
                          "method": "getSignaturesForAddress",
                          "params": [
                            "wallet123",
                            {
                              "limit": 20,
                              "commitment": "finalized"
                            }
                          ]
                        }
                        """))
                .andRespond(withSuccess("""
                        {
                          "jsonrpc": "2.0",
                          "result": [
                            {
                              "signature": "sig1",
                              "slot": 1,
                              "err": null,
                              "blockTime": 1700000000
                            },
                            {
                              "signature": "sig2",
                              "slot": 2,
                              "err": {
                                "InstructionError": [0, "Custom"]
                              },
                              "blockTime": 1700000010
                            }
                          ],
                          "id": 1
                        }
                        """, MediaType.APPLICATION_JSON));

        assertThat(client.getSignaturesForAddress("wallet123", 20))
                .containsExactly(
                        new WalletData.RecentTransaction("sig1", 1L, 1_700_000_000L, false),
                        new WalletData.RecentTransaction("sig2", 2L, 1_700_000_010L, true)
                );
        server.verify();
    }

    @Test
    void fetchesTransactionDetailsAccountKeys() {
        server.expect(requestTo("https://solana.test/"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("""
                        {
                          "jsonrpc": "2.0",
                          "method": "getTransaction",
                          "params": [
                            "sig1",
                            {
                              "encoding": "json",
                              "maxSupportedTransactionVersion": 0,
                              "commitment": "finalized"
                            }
                          ]
                        }
                        """))
                .andRespond(withSuccess("""
                        {
                          "jsonrpc": "2.0",
                          "result": {
                            "transaction": {
                              "message": {
                                "accountKeys": ["wallet123", "account1"]
                              }
                            },
                            "meta": {
                              "loadedAddresses": {
                                "writable": ["loaded1"],
                                "readonly": ["loaded2"]
                              }
                            }
                          },
                          "id": 1
                        }
                        """, MediaType.APPLICATION_JSON));

        assertThat(client.getTransactionDetails("sig1").accountKeys())
                .containsExactly("wallet123", "account1", "loaded1", "loaded2");
        server.verify();
    }

    @Test
    void jsonRpcErrorThrowsSolanaException() {
        server.expect(requestTo("https://solana.test/"))
                .andRespond(withSuccess("""
                        {
                          "jsonrpc": "2.0",
                          "error": {
                            "code": -32000,
                            "message": "upstream error"
                          },
                          "id": 1
                        }
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.getBalanceLamports("wallet123"))
                .isInstanceOf(SolanaRpcClientException.class)
                .hasMessage("Wallet data provider failed.");

        server.verify();
    }

    @Test
    void jsonRpcRateLimitRetriesThenSucceeds() {
        server.expect(requestTo("https://solana.test/"))
                .andRespond(withSuccess("""
                        {
                          "jsonrpc": "2.0",
                          "error": {
                            "code": 429,
                            "message": "Too many requests for a specific RPC call"
                          },
                          "id": 1
                        }
                        """, MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://solana.test/"))
                .andRespond(withSuccess("""
                        {
                          "jsonrpc": "2.0",
                          "result": {
                            "value": 5000000000
                          },
                          "id": 2
                        }
                        """, MediaType.APPLICATION_JSON));

        assertThat(client.getBalanceLamports("wallet123")).isEqualTo(5_000_000_000L);

        server.verify();
    }

    @Test
    void httpRateLimitUsesRetryAfterThenSucceeds() {
        server.expect(requestTo("https://solana.test/"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS)
                        .header(HttpHeaders.RETRY_AFTER, "0")
                        .body("Too many requests")
                        .contentType(MediaType.TEXT_PLAIN));
        server.expect(requestTo("https://solana.test/"))
                .andRespond(withSuccess("""
                        {
                          "jsonrpc": "2.0",
                          "result": {
                            "value": 6000000000
                          },
                          "id": 2
                        }
                        """, MediaType.APPLICATION_JSON));

        assertThat(client.getBalanceLamports("wallet123")).isEqualTo(6_000_000_000L);

        server.verify();
    }

    @Test
    void httpRateLimitUsesFallbackBackoffThenSucceeds() {
        server.expect(requestTo("https://solana.test/"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS)
                        .body("Too many requests")
                        .contentType(MediaType.TEXT_PLAIN));
        server.expect(requestTo("https://solana.test/"))
                .andRespond(withSuccess("""
                        {
                          "jsonrpc": "2.0",
                          "result": {
                            "value": 6000000000
                          },
                          "id": 2
                        }
                        """, MediaType.APPLICATION_JSON));

        assertThat(client.getBalanceLamports("wallet123")).isEqualTo(6_000_000_000L);

        server.verify();
    }

    @Test
    void exhaustedRateLimitRetriesThrowRateLimitExceptionAfterDefaultTwoAttempts() {
        server.expect(requestTo("https://solana.test/"))
                .andRespond(withSuccess("""
                        {
                          "jsonrpc": "2.0",
                          "error": {
                            "code": 429,
                            "message": "Too many requests for a specific RPC call"
                          },
                          "id": 1
                        }
                        """, MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://solana.test/"))
                .andRespond(withSuccess("""
                        {
                          "jsonrpc": "2.0",
                          "error": {
                            "code": 429,
                            "message": "Too many requests for a specific RPC call"
                          },
                          "id": 2
                        }
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.getBalanceLamports("wallet123"))
                .isInstanceOf(SolanaRpcRateLimitException.class)
                .hasMessage("Wallet data provider failed.");

        server.verify();
    }

    @Test
    void nonSuccessResponseThrowsSolanaException() {
        server.expect(requestTo("https://solana.test/"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.getBalanceLamports("wallet123"))
                .isInstanceOf(SolanaRpcClientException.class)
                .hasMessage("Wallet data provider failed.");

        server.verify();
    }

    @Test
    void missingRequiredResultFieldsThrowSolanaException() {
        server.expect(requestTo("https://solana.test/"))
                .andRespond(withSuccess("""
                        {
                          "jsonrpc": "2.0",
                          "result": {},
                          "id": 1
                        }
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.getBalanceLamports("wallet123"))
                .isInstanceOf(SolanaRpcClientException.class)
                .hasMessage("Wallet data provider failed.");

        server.verify();
    }
}
