import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { getBaseUrl } from '../API'
import { useAuth } from '../AuthContext'
import { beginDiscussion } from '../utils/discussion'

const normalizeUser = (profile, fallbackUserId) => ({
  userId: profile?.userId ?? profile?.id ?? fallbackUserId ?? '',
  pseudo: profile?.pseudo ?? '',
  nom: profile?.nom ?? '',
  prenom: profile?.prenom ?? '',
  photoProfil: profile?.photoProfil ?? '',
  deleted: profile?.deleted === true,
})

const getFullName = (profile) => [profile?.prenom, profile?.nom].filter(Boolean).join(' ').trim()

export default function ProfilePreviewCard({ userId, initialUser, align = 'left', onClose }) {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [profile, setProfile] = useState(() => normalizeUser(initialUser, userId))
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [discussionError, setDiscussionError] = useState('')
  const isOwnProfile = user?.userId === userId
  const fullName = useMemo(() => getFullName(profile), [profile])

  useEffect(() => {
    let active = true

    const fetchProfile = async () => {
      setLoading(true)
      setError('')

      try {
        const res = await fetch(`${getBaseUrl()}/user/${userId}`, {
          credentials: 'include',
        })

        if (!res.ok) {
          const text = await res.text()
          throw new Error(text || 'Failed to load profile.')
        }

        const data = await res.json()

        if (active) {
          setProfile(normalizeUser(data, userId))
        }
      } catch (err) {
        if (active) {
          setError(err.message ?? 'Failed to load profile.')
        }
      } finally {
        if (active) {
          setLoading(false)
        }
      }
    }

    fetchProfile()

    return () => {
      active = false
    }
  }, [userId])

  const handleBeginDiscussion = async () => {
    setDiscussionError('')

    try {
      const opened = await beginDiscussion({
        currentUserId: user?.userId,
        targetUserId: userId,
        navigate,
      })

      if (opened) {
        onClose?.()
      }
    } catch (err) {
      setDiscussionError(err.message ?? 'Failed to open discussion.')
    }
  }

  const handleViewProfile = () => {
    onClose?.()
    navigate(`/ProfilePage/${userId}`)
  }

  return (
    <div
      className={`profile-preview-card ${align === 'right' ? 'align-right' : ''}`.trim()}
      role="dialog"
      onClick={(event) => event.stopPropagation()}
    >
      <div className="profile-preview-top">
        {profile.photoProfil
          ? <img className="profile-preview-avatar" src={profile.photoProfil} alt={profile.pseudo || 'Profile'} />
          : <div className="profile-preview-avatar-placeholder">{profile.pseudo?.[0]?.toUpperCase() ?? 'U'}</div>
        }

        <div className="profile-preview-text">
          <p className="profile-preview-name">{profile.pseudo || 'Unknown user'}</p>
          <p className="profile-preview-subtitle">{fullName || profile.userId || 'VillagETS member'}</p>
        </div>
      </div>

      {loading && <p className="profile-preview-status">Loading profile...</p>}
      {error && <p className="profile-preview-error">{error}</p>}

      {!loading && !error && (
        <div className="profile-preview-details">
          {fullName && <p>Full name: {fullName}</p>}
          {profile.userId && <p>User ID: {profile.userId}</p>}
        </div>
      )}

      {discussionError && <p className="profile-preview-error">{discussionError}</p>}

      <div className="profile-preview-actions">
        {!isOwnProfile && !profile.deleted && (
          <button
            type="button"
            className="profile-preview-button"
            onClick={handleBeginDiscussion}
            disabled={!!error}
          >
            Begin Discussion
          </button>
        )}
        <button
          type="button"
          className="profile-preview-button secondary"
          onClick={handleViewProfile}
          disabled={!!error}
        >
          View Profile Page
        </button>
      </div>
    </div>
  )
}
