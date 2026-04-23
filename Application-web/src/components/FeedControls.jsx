import { useEffect, useRef, useState } from 'react'
import {
  DEFAULT_FEED_SORT_MODE,
  FEED_SORT_OPTIONS,
  normalizeFeedSortMode,
} from '../utils/feed'
import '../assets/FeedControls.css'

function SortDropdown({ sortMode, onSortChange }) {
  const [open, setOpen] = useState(false)
  const dropdownRef = useRef(null)
  const normalizedSortMode = normalizeFeedSortMode(sortMode)
  const selectedOption = FEED_SORT_OPTIONS.find(option => option.value === normalizedSortMode)
    ?? FEED_SORT_OPTIONS[0]

  useEffect(() => {
    const handlePointerDown = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setOpen(false)
      }
    }

    document.addEventListener('mousedown', handlePointerDown)
    return () => document.removeEventListener('mousedown', handlePointerDown)
  }, [])

  return (
    <div className="feed-sort-dropdown" ref={dropdownRef}>
      <button
        className={`feed-sort-trigger ${open ? 'open' : ''}`.trim()}
        type="button"
        aria-haspopup="menu"
        aria-expanded={open}
        onClick={() => setOpen(value => !value)}
      >
        <span className="feed-sort-value">{selectedOption.label}</span>
        <span className="feed-sort-caret" aria-hidden="true">v</span>
      </button>

      {open && (
        <div className="feed-sort-menu" role="menu">
          {FEED_SORT_OPTIONS.map(option => (
            <button
              key={option.value}
              className={`feed-sort-option ${option.value === normalizedSortMode ? 'active' : ''}`.trim()}
              type="button"
              role="menuitemradio"
              aria-checked={option.value === normalizedSortMode}
              onClick={() => {
                onSortChange?.(option.value)
                setOpen(false)
              }}
            >
              <span>{option.label}</span>
            </button>
          ))}
        </div>
      )}
    </div>
  )
}

function MarketplacePriceControls({
  minValue = null,
  maxValue = null,
  onChange,
}) {
  const [draftMin, setDraftMin] = useState(minValue == null ? '' : String(minValue))
  const [draftMax, setDraftMax] = useState(maxValue == null ? '' : String(maxValue))

  useEffect(() => {
    setDraftMin(minValue == null ? '' : String(minValue))
  }, [minValue])

  useEffect(() => {
    setDraftMax(maxValue == null ? '' : String(maxValue))
  }, [maxValue])

  const parseBound = (rawValue) => {
    const trimmedValue = rawValue.trim()
    if (trimmedValue === '') return null

    const numericValue = Number(trimmedValue)
    if (!Number.isFinite(numericValue) || numericValue < 0) {
      return null
    }

    return numericValue
  }

  const resetDraftsToAppliedValues = () => {
    setDraftMin(minValue == null ? '' : String(minValue))
    setDraftMax(maxValue == null ? '' : String(maxValue))
  }

  const parsedDraftMin = parseBound(draftMin)
  const parsedDraftMax = parseBound(draftMax)
  const hasInvalidMin = draftMin.trim() !== '' && parsedDraftMin == null
  const hasInvalidMax = draftMax.trim() !== '' && parsedDraftMax == null
  const hasInvalidRange = parsedDraftMin != null && parsedDraftMax != null && parsedDraftMin > parsedDraftMax

  const submitDraft = () => {
    const nextMin = parseBound(draftMin)
    const nextMax = parseBound(draftMax)

    if ((draftMin.trim() !== '' && nextMin == null) || (draftMax.trim() !== '' && nextMax == null)) {
      resetDraftsToAppliedValues()
      return
    }

    if (nextMin != null && nextMax != null && nextMin > nextMax) {
      resetDraftsToAppliedValues()
      return
    }

    onChange?.({
      min: nextMin,
      max: nextMax,
    })
  }

  const handleKeyDown = (event) => {
    if (event.key === 'Enter') {
      event.preventDefault()
      submitDraft()
    }
  }

  return (
    <div className="marketplace-price-controls">
      <span className="marketplace-price-label">Price</span>
      <div className="marketplace-price-inline">
        <label className="marketplace-price-field">
          <input
            type="number"
            min="0"
            placeholder="Min"
            className={hasInvalidMin || hasInvalidRange ? 'invalid' : ''}
            value={draftMin}
            onChange={event => setDraftMin(event.target.value)}
            onBlur={submitDraft}
            onKeyDown={handleKeyDown}
          />
        </label>

        <span className="marketplace-price-separator">to</span>

        <label className="marketplace-price-field">
          <input
            type="number"
            min="0"
            placeholder="Max"
            className={hasInvalidMax || hasInvalidRange ? 'invalid' : ''}
            value={draftMax}
            onChange={event => setDraftMax(event.target.value)}
            onBlur={submitDraft}
            onKeyDown={handleKeyDown}
          />
        </label>
      </div>
    </div>
  )
}

export default function FeedControls({
  sortMode = DEFAULT_FEED_SORT_MODE,
  onSortChange,
  marketplacePriceFilter = null,
}) {
  return (
    <div className={`feed-controls-shell ${marketplacePriceFilter ? 'with-marketplace' : ''}`.trim()}>
      <div className="feed-controls-top-row">
        <SortDropdown sortMode={sortMode} onSortChange={onSortChange} />
        {marketplacePriceFilter && (
          <MarketplacePriceControls
            minValue={marketplacePriceFilter.minValue}
            maxValue={marketplacePriceFilter.maxValue}
            onChange={marketplacePriceFilter.onChange}
          />
        )}
      </div>
    </div>
  )
}
