import { Link } from 'react-router-dom'
import DeferredImage from '../DeferredImage'
import '../../assets/MarketplaceCard.css'

function formatPrice(price) {
  const parsedPrice = price == null ? null : Number(price)
  if (parsedPrice == null || Number.isNaN(parsedPrice)) {
    return 'Price unavailable'
  }

  return new Intl.NumberFormat('en-CA', {
    style: 'currency',
    currency: 'CAD',
    maximumFractionDigits: parsedPrice % 1 === 0 ? 0 : 2,
  }).format(parsedPrice)
}

export default function MarketplaceCard({ post }) {
  const media = (post.media ?? []).filter(url => typeof url === 'string' && url.trim().length > 0)
  const previewImage = media[0] ?? null
  const posterName = post.op?.pseudo ?? 'Unknown'

  return (
    <Link to={`/post/${post.id}`} className="marketplace-card-link">
      <article className="marketplace-card">
        <div className="marketplace-card-media">
          {previewImage ? (
            <DeferredImage
              src={previewImage}
              alt={post.title || 'Marketplace item'}
              className="marketplace-card-image"
              placeholderClassName="marketplace-card-placeholder"
              placeholderText=""
            />
          ) : (
            <div className="marketplace-card-placeholder">No image</div>
          )}
        </div>

        <div className="marketplace-card-body">
          <p className="marketplace-card-price">{formatPrice(post.prix)}</p>
          <h3 className="marketplace-card-title">{post.title || 'Untitled item'}</h3>
          <p className="marketplace-card-poster">{posterName}</p>
        </div>
      </article>
    </Link>
  )
}
