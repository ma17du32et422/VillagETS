import { useEffect, useRef, useState } from 'react'
import ProfilePreviewCard from './ProfilePreviewCard'
import '../assets/ProfilePreview.css'

const getUserId = (user) => user?.id ?? user?.userId ?? null
const getUserPseudo = (user) => user?.pseudo ?? 'Unknown user'

export default function ProfileAvatar({
  user,
  imageProps = {},
  placeholderProps = {},
  align = 'left',
  anchorClassName = '',
}) {
  const [open, setOpen] = useState(false)
  const anchorRef = useRef(null)
  const userId = getUserId(user)

  useEffect(() => {
    if (!open) return

    const handleMouseDown = (event) => {
      if (anchorRef.current && !anchorRef.current.contains(event.target))
        setOpen(false)
    }

    const handleKeyDown = (event) => {
      if (event.key === 'Escape')
        setOpen(false)
    }

    document.addEventListener('mousedown', handleMouseDown)
    document.addEventListener('keydown', handleKeyDown)

    return () => {
      document.removeEventListener('mousedown', handleMouseDown)
      document.removeEventListener('keydown', handleKeyDown)
    }
  }, [open])

  const avatar = user?.photoProfil
    ? <img {...imageProps} src={user.photoProfil} alt={getUserPseudo(user)} />
    : <div {...placeholderProps}>{getUserPseudo(user)?.[0]?.toUpperCase() ?? 'U'}</div>

  if (!userId) return avatar

  return (
    <div className={`profile-avatar-anchor ${anchorClassName}`.trim()} ref={anchorRef}>
      <button
        type="button"
        className="profile-avatar-trigger"
        onClick={(event) => {
          event.stopPropagation()
          setOpen((current) => !current)
        }}
        aria-haspopup="dialog"
        aria-expanded={open}
      >
        {avatar}
      </button>

      {open && (
        <ProfilePreviewCard
          userId={userId}
          initialUser={user}
          align={align}
          onClose={() => setOpen(false)}
        />
      )}
    </div>
  )
}
