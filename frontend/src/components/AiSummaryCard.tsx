"use client";

import { useMemo, useState } from "react";
import type { ReactNode } from "react";

type AiSummaryCardProps = {
  summary: string;
};

type MarkdownBlock =
  | {
      content: string;
      level: number;
      type: "heading";
    }
  | {
      content: string;
      type: "paragraph";
    }
  | {
      items: string[];
      type: "list";
    };

type SummaryPreviewContent = {
  content: string;
  heading: string;
};

const SECTION_TITLES = new Set([
  "Overall Assessment",
  "Key Trust Signals",
  "Key Risk Signals",
  "Final Summary"
]);

export function AiSummaryCard({ summary }: AiSummaryCardProps) {
  const [isExpanded, setIsExpanded] = useState(false);
  const blocks = useMemo(() => parseMarkdownBlocks(summary), [summary]);
  const preview = useMemo(() => buildSummaryPreview(blocks), [blocks]);

  return (
    <article className="w-full min-w-0 rounded-lg border border-line bg-white/[0.045] p-5">
      <p className="text-sm text-slate-400">AI Summary</p>
      <h2 className="mt-1 text-xl font-semibold text-white">Trust intelligence</h2>
      <div className="transition-opacity duration-200">
        {isExpanded ? (
          <MarkdownSummary blocks={blocks} />
        ) : (
          <SummaryPreview preview={preview} />
        )}
      </div>
      <button
        aria-expanded={isExpanded}
        className="mt-5 inline-flex min-h-10 items-center justify-center rounded-lg border border-solana-cyan/30 bg-solana-cyan/10 px-3 text-sm font-semibold text-cyan-100 transition hover:border-solana-cyan/60 hover:bg-solana-cyan/15 hover:text-white"
        onClick={() => setIsExpanded((current) => !current)}
        type="button"
      >
        {isExpanded ? "Collapse" : "View full analysis"}
      </button>
    </article>
  );
}

function MarkdownSummary({ blocks }: { blocks: MarkdownBlock[] }) {
  return (
    <div className="mt-4 min-w-0 space-y-4 break-words text-base leading-7 text-slate-300">
      {blocks.map((block, index) => renderBlock(block, index))}
    </div>
  );
}

function SummaryPreview({ preview }: { preview: SummaryPreviewContent }) {
  return (
    <div className="mt-4 min-w-0 space-y-3 break-words text-base leading-7 text-slate-300">
      <h4 className="text-sm font-semibold uppercase tracking-[0.16em] text-solana-cyan">
        {preview.heading}
      </h4>
      <p className="line-clamp-3">{renderInlineMarkdown(preview.content)}</p>
    </div>
  );
}

function parseMarkdownBlocks(markdown: string): MarkdownBlock[] {
  const blocks: MarkdownBlock[] = [];
  const pendingListItems: string[] = [];

  function flushList() {
    if (pendingListItems.length > 0) {
      blocks.push({
        items: [...pendingListItems],
        type: "list"
      });
      pendingListItems.length = 0;
    }
  }

  markdown
    .split(/\r?\n/)
    .map((line) => line.trim())
    .forEach((line) => {
      if (!line) {
        flushList();
        return;
      }

      const bulletMatch = line.match(/^[-*]\s+(.+)$/);

      if (bulletMatch) {
        pendingListItems.push(bulletMatch[1]);
        return;
      }

      flushList();

      const markdownHeadingMatch = line.match(/^(#{1,6})\s+(.+)$/);

      if (markdownHeadingMatch) {
        blocks.push({
          content: stripInlineMarkdown(markdownHeadingMatch[2]),
          level: Math.min(markdownHeadingMatch[1].length, 4),
          type: "heading"
        });
        return;
      }

      const sectionMatch = line.match(
        /^\d+\.\s+(?:\*\*)?([^:*]+?)(?:\*\*)?(?::\s*(.+))?$/
      );

      if (sectionMatch && SECTION_TITLES.has(sectionMatch[1].trim())) {
        blocks.push({
          content: sectionMatch[1].trim(),
          level: 3,
          type: "heading"
        });

        if (sectionMatch[2]) {
          blocks.push({
            content: sectionMatch[2].trim(),
            type: "paragraph"
          });
        }

        return;
      }

      blocks.push({
        content: line,
        type: "paragraph"
      });
    });

  flushList();

  return blocks;
}

function buildSummaryPreview(blocks: MarkdownBlock[]): SummaryPreviewContent {
  const fallbackParagraph = firstParagraphContent(blocks);
  const overallAssessmentIndex = blocks.findIndex(
    (block) =>
      block.type === "heading" &&
      block.content.trim().toLowerCase() === "overall assessment"
  );

  if (overallAssessmentIndex >= 0) {
    const overallAssessmentContent = firstParagraphContent(
      blocks.slice(overallAssessmentIndex + 1)
    );

    return {
      content: overallAssessmentContent || fallbackParagraph,
      heading: "Overall Assessment"
    };
  }

  return {
    content: fallbackParagraph || "No AI summary preview is available yet.",
    heading: "Overall Assessment"
  };
}

function firstParagraphContent(blocks: MarkdownBlock[]) {
  for (const block of blocks) {
    if (block.type === "paragraph" && block.content.trim()) {
      return block.content.trim();
    }
  }

  return "";
}

function renderBlock(block: MarkdownBlock, index: number) {
  if (block.type === "heading") {
    const Tag = block.level <= 2 ? "h3" : "h4";

    return (
      <Tag
        className="text-sm font-semibold uppercase tracking-[0.16em] text-solana-cyan"
        key={`${block.type}-${index}`}
      >
        {block.content}
      </Tag>
    );
  }

  if (block.type === "list") {
    return (
      <ul className="space-y-2" key={`${block.type}-${index}`}>
        {block.items.map((item, itemIndex) => (
          <li className="flex gap-3" key={`${item}-${itemIndex}`}>
            <span className="mt-3 size-1.5 shrink-0 rounded-full bg-solana-cyan" />
            <span>{renderInlineMarkdown(item)}</span>
          </li>
        ))}
      </ul>
    );
  }

  return <p key={`${block.type}-${index}`}>{renderInlineMarkdown(block.content)}</p>;
}

function renderInlineMarkdown(text: string): ReactNode[] {
  return text.split(/(\*\*[^*]+\*\*|`[^`]+`)/g).map((part, index) => {
    if (part.startsWith("**") && part.endsWith("**")) {
      return (
        <strong className="font-semibold text-white" key={`${part}-${index}`}>
          {part.slice(2, -2)}
        </strong>
      );
    }

    if (part.startsWith("`") && part.endsWith("`")) {
      return (
        <code
          className="break-all rounded border border-slate-700 bg-slate-950/80 px-1.5 py-0.5 text-sm text-solana-cyan"
          key={`${part}-${index}`}
        >
          {part.slice(1, -1)}
        </code>
      );
    }

    return part;
  });
}

function stripInlineMarkdown(text: string) {
  return text.replace(/\*\*([^*]+)\*\*/g, "$1").replace(/`([^`]+)`/g, "$1");
}
