import { useEffect, useMemo, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { getBaseUrl } from '../API'
import { useAuth } from '../AuthContext'
import { beginDiscussion } from '../utils/discussion'
import Post from '../components/subcomponents/Post'
import '../assets/ProfilePreview.css'
import '../assets/UserProfilePage.css'
import usePageTitle from "../utils/usePageTitle.js";

const normalizeUser = (profile, fallbackUserId) => ({
  userId: profile?.userId ?? profile?.id ?? fallbackUserId ?? '',
  pseudo: profile?.pseudo ?? '',
  nom: profile?.nom ?? '',
  prenom: profile?.prenom ?? '',
  photoProfil: profile?.photoProfil ?? '',
  deleted: profile?.deleted === true,
})

const getFullName = (profile) => [profile?.prenom, profile?.nom].filter(Boolean).join(' ').trim()

function UserProfilePage() {
  const { userId } = useParams()
  const navigate = useNavigate()
  const { user } = useAuth()
  const [profile, setProfile] = useState(null)
  const [posts, setPosts] = useState([])
  const [postsLoading, setPostsLoading] = useState(true)
  const [postsError, setPostsError] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [discussionError, setDiscussionError] = useState('')
  const [deleteError, setDeleteError] = useState('')
  const [deleteSuccess, setDeleteSuccess] = useState('')
  const [isDeleting, setIsDeleting] = useState(false)
  const [isWiping, setIsWiping] = useState(false)
  const isOwnProfile = user?.userId === userId
  const canDeleteUser = user?.mainAdmin === true && !isOwnProfile && profile?.deleted !== true
  const canWipeUser = user?.mainAdmin === true && !isOwnProfile
  const fullName = useMemo(() => getFullName(profile), [profile])

  usePageTitle(profile?.pseudo ?? '');

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
          throw new Error(text || 'Unable to load profile.')
        }

        const data = await res.json()

        if (active) {
          setProfile(normalizeUser(data, userId))
        }
      } catch (err) {
        if (active) {
          setError(err.message ?? 'Unable to load profile.')
          setProfile(null)
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

  useEffect(() => {
    let active = true

    const fetchPosts = async () => {
      setPostsLoading(true)
      setPostsError('')

      try {
        const res = await fetch(`${getBaseUrl()}/user/${userId}/posts`, {
          credentials: 'include',
        })

        if (!res.ok) {
          const text = await res.text()
          throw new Error(text || 'Unable to load posts.')
        }

        const data = await res.json()

        if (active) {
          setPosts((data ?? []).map((post) => ({
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
          })))
        }
      } catch (err) {
        if (active) {
          setPosts([])
          setPostsError(err.message ?? 'Unable to load posts.')
        }
      } finally {
        if (active) {
          setPostsLoading(false)
        }
      }
    }

    fetchPosts()

    return () => {
      active = false
    }
  }, [userId])

  const handleBeginDiscussion = async () => {
    setDiscussionError('')

    try {
      await beginDiscussion({
        currentUserId: user?.userId,
        targetUserId: userId,
        navigate,
      })
    } catch (err) {
      setDiscussionError(err.message ?? 'Failed to open discussion.')
    }
  }

  const handleDeleteUser = async () => {
    if (!canDeleteUser || isDeleting) return

    const confirmed = window.confirm(`Delete ${profile?.pseudo || 'this user'}? Their posts will be removed and their comments, reactions, and messages will remain as [deleted user].`)
    if (!confirmed) return

    setDeleteError('')
    setDeleteSuccess('')
    setIsDeleting(true)

    try {
      const res = await fetch(`${getBaseUrl()}/user/${userId}`, {
        method: 'DELETE',
        credentials: 'include',
      })

      if (!res.ok) {
        const text = await res.text()
        throw new Error(text || 'Failed to delete user.')
      }

      setProfile((current) => normalizeUser({
        ...current,
        pseudo: '[deleted user]',
        nom: '',
        prenom: '',
        photoProfil: '',
        deleted: true,
      }, userId))
      setPosts([])
      setDeleteSuccess('User deleted.')
    } catch (err) {
      setDeleteError(err.message ?? 'Failed to delete user.')
    } finally {
      setIsDeleting(false)
    }
  }

  const handleWipeUser = async () => {
    if (!canWipeUser || isWiping) return

    const confirmed = window.confirm(`Wipe ${profile?.pseudo || 'this user'} permanently? This removes their account, posts, comments, reactions, messages, and discussions.`)
    if (!confirmed) return

    setDeleteError('')
    setDeleteSuccess('')
    setIsWiping(true)

    try {
      const res = await fetch(`${getBaseUrl()}/user/${userId}/wipe`, {
        method: 'DELETE',
        credentials: 'include',
      })

      if (!res.ok) {
        const text = await res.text()
        throw new Error(text || 'Failed to wipe user.')
      }

      setProfile(null)
      setPosts([])
      setError('User has been permanently deleted.')
      setDeleteSuccess('User wiped.')
    } catch (err) {
      setDeleteError(err.message ?? 'Failed to wipe user.')
    } finally {
      setIsWiping(false)
    }
  }

  return (
      <main className="user-profile-page">
        <section className="user-profile-hero">
          <div className="user-profile-shell user-profile-head">
            {loading && <p>Loading profile...</p>}
            {!loading && error && <p>{error}</p>}

            {!loading && !error && profile && (
              <>
                <div className="user-profile-avatar-wrap">
                  {profile.photoProfil
                    ? <img className="profile-preview-avatar large" src={profile.photoProfil} alt={profile.pseudo || 'Profile'} />
                    : <div className="profile-preview-avatar-placeholder large">{profile.pseudo?.[0]?.toUpperCase() ?? 'U'}</div>
                  }
                </div>

                <div className="user-profile-summary">
                  <div className="user-profile-title-row">
                    <h2>{profile.pseudo || 'Unknown user'}</h2>

                    {!isOwnProfile && !profile.deleted && (
                      <button className="user-profile-discussion-button" type="button" onClick={handleBeginDiscussion}>
                        Begin Discussion
                      </button>
                    )}

                    {canDeleteUser && (
                      <button
                        className="user-profile-delete-button"
                        type="button"
                        onClick={handleDeleteUser}
                        disabled={isDeleting}
                      >
                        {isDeleting ? 'Deleting...' : 'Delete User'}
                      </button>
                    )}

                    {canWipeUser && (
                      <button
                        className="user-profile-wipe-button"
                        type="button"
                        onClick={handleWipeUser}
                        disabled={isWiping}
                      >
                        {isWiping ? 'Wiping...' : 'Wipe User'}
                      </button>
                    )}
                  </div>

                  <div className="user-profile-stats">
                    <span><strong>{posts.length}</strong> posts</span>
                    {fullName && <span>{fullName}</span>}
                  </div>

                  <div className="user-profile-details">
                    {profile.userId && <p>User ID: {profile.userId}</p>}
                    {profile.deleted && <p>This account has been deleted.</p>}
                  </div>

                  {discussionError && <p className="profile-preview-error">{discussionError}</p>}
                  {deleteError && <p className="profile-preview-error">{deleteError}</p>}
                  {deleteSuccess && <p className="profile-preview-success">{deleteSuccess}</p>}
                </div>
              </>
            )}
          </div>
        </section>

        <section className="user-profile-posts-section">
          <div className="user-profile-shell user-profile-posts">
            <h3>{isOwnProfile ? 'My Posts' : `${profile?.pseudo ?? 'User'}'s Posts`}</h3>

            {postsLoading && <p>Loading posts...</p>}
            {!postsLoading && postsError && <p>{postsError}</p>}
            {!postsLoading && !postsError && posts.length === 0 && <p className="user-profile-empty">No posts yet.</p>}
            {!postsLoading && !postsError && (
              <div className="user-profile-post-list">
                {posts.map((post) => (
                  <Post key={post.id} post={post} />
                ))}
              </div>
            )}
          </div>
        </section>
      </main>
  )
}

export default UserProfilePage
