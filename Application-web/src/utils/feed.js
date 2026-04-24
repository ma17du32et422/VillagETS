import { getBaseUrl } from '../API'

export const DEFAULT_FEED_SORT_MODE = 'DESC'
export const FEED_PAGE_SIZE = 20

export const FEED_SORT_OPTIONS = [
  { value: 'DESC', label: 'Newest' },
  { value: 'ASC', label: 'Oldest' },
  { value: 'TOP', label: 'Top' },
]

export function normalizeFeedSortMode(mode) {
  return FEED_SORT_OPTIONS.some(option => option.value === mode)
    ? mode
    : DEFAULT_FEED_SORT_MODE
}

export function mapFeedPost(post) {
  return {
    id: post.id,
    title: post.titre ?? '',
    contents: post.contenu ?? '',
    op: post.op ?? { id: null, pseudo: 'Unknown', photoProfil: null },
    datetime: post.datePublication ?? '',
    media: post.media ?? [],
    tags: post.tags ?? [],
    prix: post.prix ?? null,
    articleAVendre: post.articleAVendre ?? false,
    likes: post.likes ?? 0,
    dislikes: post.dislikes ?? 0,
    commentaires: post.commentaires ?? 0,
    userReaction: post.userReaction ?? null,
    comments: [],
  }
}

function getDateValue(value) {
  const timestamp = new Date(value ?? '').getTime()
  return Number.isFinite(timestamp) ? timestamp : 0
}

export function sortFeedPosts(posts, sortMode) {
  const normalizedSortMode = normalizeFeedSortMode(sortMode)
  const sortedPosts = [...posts]

  sortedPosts.sort((left, right) => {
    if (normalizedSortMode === 'TOP' && right.likes !== left.likes) {
      return right.likes - left.likes
    }

    const dateDiff = getDateValue(right.datetime) - getDateValue(left.datetime)
    if (normalizedSortMode === 'ASC') {
      return -dateDiff || (left.id ?? 0) - (right.id ?? 0)
    }

    return dateDiff || (right.id ?? 0) - (left.id ?? 0)
  })

  return sortedPosts
}

export async function requestFeed({
  searchString = null,
  isMarketplace = false,
  minPrice = null,
  maxPrice = null,
  pageIndex = 0,
  sortMode = DEFAULT_FEED_SORT_MODE,
}) {
  const response = await fetch(`${getBaseUrl()}/feed`, {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      searchString,
      tags: null,
      isMarketplace,
      minPrice,
      maxPrice,
      pageIndex,
      sortMode: normalizeFeedSortMode(sortMode),
    }),
  })

  if (!response.ok) {
    throw new Error('Failed to fetch posts')
  }

  const data = await response.json()
  return (data ?? []).map(mapFeedPost)
}

export function clampPrice(value, min, max) {
  return Math.min(Math.max(value, min), max)
}

export function roundPriceBound(value) {
  if (!Number.isFinite(value) || value <= 0) return 100
  return Math.ceil(value / 100) * 100
}

export function mergeFeedPosts(existingPosts, incomingPosts) {
  return Array.from(
    new Map([...existingPosts, ...incomingPosts].map(post => [post.id, post])).values()
  )
}
