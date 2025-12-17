import type { Genre, ReadingStatus } from '../types';

export const GENRES: { value: Genre; label: string }[] = [
  { value: 'FICTION', label: 'Fiction' },
  { value: 'NON_FICTION', label: 'Non-Fiction' },
  { value: 'MYSTERY', label: 'Mystery' },
  { value: 'SCIENCE_FICTION', label: 'Science Fiction' },
  { value: 'FANTASY', label: 'Fantasy' },
  { value: 'ROMANCE', label: 'Romance' },
  { value: 'THRILLER', label: 'Thriller' },
  { value: 'BIOGRAPHY', label: 'Biography' },
  { value: 'HISTORY', label: 'History' },
  { value: 'SCIENCE', label: 'Science' },
  { value: 'SELF_HELP', label: 'Self Help' },
  { value: 'POETRY', label: 'Poetry' },
  { value: 'DRAMA', label: 'Drama' },
  { value: 'HORROR', label: 'Horror' },
  { value: 'ADVENTURE', label: 'Adventure' },
  { value: 'CHILDREN', label: 'Children' },
  { value: 'YOUNG_ADULT', label: 'Young Adult' },
  { value: 'COMICS', label: 'Comics' },
  { value: 'ART', label: 'Art' },
  { value: 'COOKING', label: 'Cooking' },
  { value: 'TRAVEL', label: 'Travel' },
  { value: 'RELIGION', label: 'Religion' },
  { value: 'PHILOSOPHY', label: 'Philosophy' },
  { value: 'PSYCHOLOGY', label: 'Psychology' },
  { value: 'BUSINESS', label: 'Business' },
  { value: 'TECHNOLOGY', label: 'Technology' },
  { value: 'OTHER', label: 'Other' },
];

export const READING_STATUSES: { value: ReadingStatus; label: string; color: string }[] = [
  { value: 'TO_READ', label: 'To Read', color: 'bg-gray-100 text-gray-800' },
  { value: 'READING', label: 'Reading', color: 'bg-blue-100 text-blue-800' },
  { value: 'COMPLETED', label: 'Completed', color: 'bg-green-100 text-green-800' },
  { value: 'ON_HOLD', label: 'On Hold', color: 'bg-yellow-100 text-yellow-800' },
  { value: 'DROPPED', label: 'Dropped', color: 'bg-red-100 text-red-800' },
];

export const getGenreLabel = (genre: Genre): string => {
  return GENRES.find((g) => g.value === genre)?.label || genre;
};

export const getStatusLabel = (status: ReadingStatus): string => {
  return READING_STATUSES.find((s) => s.value === status)?.label || status;
};

export const getStatusColor = (status: ReadingStatus): string => {
  return READING_STATUSES.find((s) => s.value === status)?.color || 'bg-gray-100 text-gray-800';
};