import { useEffect, useRef, useState } from 'react'
import { createPortal } from 'react-dom'
import ProfilePreviewCard from './ProfilePreviewCard'
import '../assets/ProfilePreview.css'

const getUserId = (user) => user?.id ?? user?.userId ?? null
const getUserPseudo = (user) => user?.pseudo ?? 'Unknown user'
const CARD_WIDTH = 260
const CARD_GAP = 8
const VIEWPORT_PADDING = 8

export default function ProfileAvatar({
  user,
  imageProps = {},
  placeholderProps = {},
  align = 'left',
  anchorClassName = '',
}) {
  const [open, setOpen] = useState(false)
  const [cardStyle, setCardStyle] = useState(null)
  const anchorRef = useRef(null)
  const portalRef = useRef(null)
  const userId = getUserId(user)

  useEffect(() => {
    if (!open) return

    const updatePosition = () => {
      const anchor = anchorRef.current
      if (!anchor) return

      const rect = anchor.getBoundingClientRect()
      const viewportWidth = window.innerWidth
      const viewportHeight = window.innerHeight
      const naturalLeft = align === 'right'
        ? rect.right - CARD_WIDTH
        : rect.left

      const maxLeft = Math.max(VIEWPORT_PADDING, viewportWidth - CARD_WIDTH - VIEWPORT_PADDING)
      const left = Math.min(Math.max(naturalLeft, VIEWPORT_PADDING), maxLeft)
      const preferredTop = rect.bottom + CARD_GAP
      const top = preferredTop + 220 > viewportHeight && rect.top > 220
        ? Math.max(VIEWPORT_PADDING, rect.top - 220 - CARD_GAP)
        : preferredTop

      setCardStyle({
        top,
        left,
      })
    }

    updatePosition()
    window.addEventListener('resize', updatePosition)
    window.addEventListener('scroll', updatePosition, true)

    return () => {
      window.removeEventListener('resize', updatePosition)
      window.removeEventListener('scroll', updatePosition, true)
    }
  }, [align, open])

  useEffect(() => {
    if (!open) return

    const handleMouseDown = (event) => {
      const clickedAnchor = anchorRef.current?.contains(event.target)
      const clickedPortal = portalRef.current?.contains(event.target)

      if (!clickedAnchor && !clickedPortal)
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
        createPortal(
          <div
            className="profile-preview-portal"
            ref={portalRef}
            style={cardStyle ?? undefined}
          >
            <ProfilePreviewCard
              userId={userId}
              initialUser={user}
              onClose={() => setOpen(false)}
            />
          </div>,
          document.body
        )
      )}
    </div>
  )
}
