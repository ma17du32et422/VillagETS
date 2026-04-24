import { useEffect, useRef, useState } from 'react'
//  DOES THIS EVEN WORK???? IDKKKKKK
export default function DeferredImage({
  src,
  alt,
  className,
  placeholderClassName,
  placeholderText = 'No image',
}) {
  const containerRef = useRef(null)
  const [shouldLoad, setShouldLoad] = useState(false)
  const [hasError, setHasError] = useState(false)

  useEffect(() => {
    const node = containerRef.current
    if (!node || !src) return

    const scrollRoot = node.closest('.home-flux-container')
    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0]?.isIntersecting) {
          setShouldLoad(true)
          observer.disconnect()
        }
      },
      {
        root: scrollRoot,
        rootMargin: '0px 0px 500px 0px',
      }
    )

    observer.observe(node)
    return () => observer.disconnect()
  }, [src])

  return (
    <div ref={containerRef} className={placeholderClassName}>
      {src && shouldLoad && !hasError ? (
        <img
          src={src}
          alt={alt}
          className={className}
          loading="lazy"
          decoding="async"
          fetchPriority="low"
          onError={() => setHasError(true)}
        />
      ) : (
        <div className="marketplace-card-placeholder-content">{placeholderText}</div>
      )}
    </div>
  )
}
