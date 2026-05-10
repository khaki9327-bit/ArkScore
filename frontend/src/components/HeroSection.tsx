import type { ReactNode } from "react";

type HeroSectionProps = {
  children: ReactNode;
};

export function HeroSection({ children }: HeroSectionProps) {
  return (
    <section className="relative w-full overflow-hidden">
      <div className="absolute inset-0 -z-10 bg-grid" />
      <div className="mx-auto w-full max-w-7xl px-4 py-10 sm:px-6 sm:py-12 lg:px-8 lg:py-16">
        {children}
      </div>
    </section>
  );
}
