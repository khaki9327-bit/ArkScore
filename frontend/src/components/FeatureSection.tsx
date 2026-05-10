const features = [
  {
    title: "Behavior intelligence",
    description:
      "Classifies long-term holding, organic activity, transaction rhythm, and token diversity signals."
  },
  {
    title: "Risk scoring",
    description:
      "Surfaces bot, rug, sybil, and meme exposure signals in a compact reputation layer."
  },
  {
    title: "AI wallet summary",
    description:
      "Turns raw onchain patterns into a readable trust narrative for product and protocol teams."
  }
];

export function FeatureSection() {
  return (
    <section className="w-full overflow-hidden border-t border-line py-12">
      <div className="mx-auto w-full max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="max-w-2xl min-w-0">
          <p className="text-sm text-slate-400">Infrastructure primitives</p>
          <h2 className="mt-2 text-3xl font-semibold text-white">
            Wallet reputation for crypto-native products
          </h2>
        </div>

        <div className="mt-8 grid min-w-0 gap-4 md:grid-cols-3">
          {features.map((feature) => (
            <article
              className="min-w-0 rounded-lg border border-line bg-white/[0.035] p-5"
              key={feature.title}
            >
              <h3 className="text-lg font-semibold text-white">
                {feature.title}
              </h3>
              <p className="mt-3 break-words leading-7 text-slate-400">
                {feature.description}
              </p>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}
