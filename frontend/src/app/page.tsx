import { FeatureSection } from "@/components/FeatureSection";
import { HeroSection } from "@/components/HeroSection";
import { WalletAnalyzer } from "@/components/WalletAnalyzer";

export default function Home() {
  return (
    <main className="w-full overflow-hidden">
      <HeroSection>
        <WalletAnalyzer />
      </HeroSection>
      <FeatureSection />
    </main>
  );
}
