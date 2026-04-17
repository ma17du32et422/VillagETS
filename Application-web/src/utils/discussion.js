import { getBaseUrl } from '../API'

export async function beginDiscussion({ currentUserId, targetUserId, navigate }) {
  if (!currentUserId) {
    navigate('/LoginPage')
    return false
  }

  if (!targetUserId) {
    throw new Error('User not found.')
  }

  if (currentUserId === targetUserId) {
    throw new Error('You cannot start a discussion with yourself.')
  }

  const res = await fetch(`${getBaseUrl()}/chat/history/${targetUserId}`, {
    credentials: 'include',
  })

  if (!res.ok) {
    const text = await res.text()
    throw new Error(text || 'Failed to open discussion.')
  }

  navigate(`/MsgPage?userId=${encodeURIComponent(targetUserId)}`)
  return true
}
