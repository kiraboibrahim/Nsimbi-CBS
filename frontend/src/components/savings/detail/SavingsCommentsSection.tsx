'use client';

import React, { useState } from 'react';
import { formatDate } from '@/lib/formatters';
import { Input } from '@/components/ui/Input';
import { Button } from '@/components/ui/Button';
import { MessageSquare } from 'lucide-react';

export interface NoteItem {
  id: number;
  note: string;
  createdOn: string | number[];
  createdByUsername: string;
}

interface SavingsCommentsSectionProps {
  notes: NoteItem[] | undefined;
  onAddNote: (note: string) => void;
  isPosting: boolean;
}

export function SavingsCommentsSection({
  notes,
  onAddNote,
  isPosting,
}: SavingsCommentsSectionProps) {
  const [newNoteText, setNewNoteText] = useState('');

  const handlePost = () => {
    if (!newNoteText.trim() || isPosting) return;
    onAddNote(newNoteText.trim());
    setNewNoteText('');
  };

  return (
    <div className="w-full px-6 py-4 border-t border-border space-y-3">
      <div className="flex items-center justify-between">
        <div className="text-xs font-semibold uppercase tracking-wider text-muted-foreground flex items-center gap-1.5">
          <MessageSquare className="w-3.5 h-3.5 text-primary" />
          Comments & Operating Notes
        </div>
        <span className="text-xs text-muted-foreground">
          {notes ? `${notes.length} note(s)` : '0 notes'}
        </span>
      </div>

      {notes && notes.length > 0 ? (
        <div className="space-y-2 max-h-36 overflow-y-auto pr-1">
          {notes.map((n) => (
            <div
              key={n.id}
              className="p-2.5 rounded-lg bg-muted/30 border border-border text-xs flex justify-between items-start gap-3"
            >
              <p className="text-foreground">{n.note}</p>
              <span className="text-[10px] text-muted-foreground font-mono flex-shrink-0">
                {formatDate(n.createdOn)} • {n.createdByUsername}
              </span>
            </div>
          ))}
        </div>
      ) : (
        <p className="text-xs text-muted-foreground italic">No comments recorded on this account.</p>
      )}

      <div className="flex items-center gap-2 pt-1">
        <Input
          type="text"
          placeholder="Add an operational note..."
          value={newNoteText}
          onChange={(e) => setNewNoteText(e.target.value)}
          className="text-xs py-1.5 h-8"
          onKeyDown={(e) => {
            if (e.key === 'Enter') {
              handlePost();
            }
          }}
        />
        <Button
          size="sm"
          variant="outline"
          disabled={!newNoteText.trim() || isPosting}
          onClick={handlePost}
          className="h-8 text-xs font-semibold px-3"
        >
          {isPosting ? 'Posting...' : 'Post Note'}
        </Button>
      </div>
    </div>
  );
}
