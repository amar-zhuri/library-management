import type { Genre, ReadingStatus } from '../types'

export const API_BASE_URL = 'http://localhost:8081/api'
export const TOKEN_STORAGE_KEY = 'lm.token'
export const USER_STORAGE_KEY = 'lm.user'

export const GENRES: Genre[] = [
  'FICTION',
  'NON_FICTION',
  'MYSTERY',
  'SCIENCE_FICTION',
  'FANTASY',
  'ROMANCE',
  'THRILLER',
  'BIOGRAPHY',
  'HISTORY',
  'SCIENCE',
  'SELF_HELP',
  'POETRY',
  'DRAMA',
  'HORROR',
  'ADVENTURE',
  'CHILDREN',
  'YOUNG_ADULT',
  'COMICS',
  'ART',
  'COOKING',
  'TRAVEL',
  'RELIGION',
  'PHILOSOPHY',
  'PSYCHOLOGY',
  'BUSINESS',
  'TECHNOLOGY',
  'OTHER',
]

export const READING_STATUSES: ReadingStatus[] = ['TO_READ', 'READING', 'COMPLETED', 'ON_HOLD', 'DROPPED']

export const DEFAULT_PAGE_SIZE = 10
