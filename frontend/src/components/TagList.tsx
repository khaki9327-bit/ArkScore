type TagListProps = {
  tags: string[];
};

export function TagList({ tags }: TagListProps) {
  return (
    <div className="flex min-w-0 flex-wrap gap-2">
      {tags.map((tag) => (
        <span
          className="max-w-full break-words rounded-lg border border-solana-cyan/20 bg-solana-cyan/10 px-3 py-2 text-sm font-medium text-cyan-100"
          key={tag}
        >
          {tag}
        </span>
      ))}
    </div>
  );
}
