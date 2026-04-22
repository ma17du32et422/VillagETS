import { useState, useEffect, useRef } from 'react';
import { useAuth } from '../AuthContext';
import { getBaseUrl } from '../API';
import ProfileAvatar from './ProfileAvatar';
import '../assets/CreatePost.css';

export default function CreatePost({ onSuccess, onCancel }) {
  const { user } = useAuth();

  const [title, setTitle] = useState("");
  const [imageFiles, setImageFiles] = useState([]);
  const [imagePreviews, setImagePreviews] = useState([]);
  const [tags, setTags] = useState("");
  const [textContent, setTextContent] = useState("");
  const [isMarketplaceItem, setIsMarketplaceItem] = useState(false);
  const [price, setPrice] = useState("");
  const [error, setError] = useState("");
  const [mediaIndex, setMediaIndex] = useState(0);
  const [dragging, setDragging] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const isSubmittingRef = useRef(false);
  const fileInputRef = useRef(null);

  const handleImageFile = (files) => {
    const arr = Array.from(files ?? []);
    setImageFiles(arr);
    setImagePreviews(arr.map(f => URL.createObjectURL(f)));
    setMediaIndex(0);
  };

  const handleDrop = (e) => {
    e.preventDefault();
    if (isSubmittingRef.current) return;
    setDragging(false);
    handleImageFile(e.dataTransfer.files);
  };

  useEffect(() => {
    return () => imagePreviews.forEach(url => URL.revokeObjectURL(url));
  }, [imagePreviews]);

  const submit = async () => {
    if (isSubmittingRef.current) return;

    setError("");
    if (!title.trim() || !textContent.trim()) {
      setError('Title and text are required.');
      return;
    }

    const parsedPrice = price.trim() === '' ? null : Number(price);
    if (isMarketplaceItem && (parsedPrice == null || Number.isNaN(parsedPrice) || parsedPrice < 0)) {
      setError('A valid price is required for items for sale.');
      return;
    }

    try {
      isSubmittingRef.current = true;
      setIsSubmitting(true);
      let mediaUrls = [];
      if (imageFiles.length > 0) {
        const uploads = await Promise.all(imageFiles.map(async (file) => {
          const form = new FormData();
          form.append('file', file);
          form.append('nom', file.name);
          form.append('type', file.type);
          const res = await fetch(`${getBaseUrl()}/upload`, {
            method: 'POST',
            credentials: 'include',
            body: form,
          });
          if (!res.ok) throw new Error(`Upload failed for ${file.name}`);
          const data = await res.json();
          return data.url;
        }));
        mediaUrls = uploads;
      }

      const response = await fetch(`${getBaseUrl()}/post`, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          nom: title,
          contenu: textContent,
          media: mediaUrls,
          articleAVendre: isMarketplaceItem,
          prix: isMarketplaceItem ? parsedPrice : null,
          tags: tags.split(',').map(t => t.trim()).filter(Boolean),
        }),
      });

      if (!response.ok) {
        const message = await response.text();
        throw new Error(message || 'Failed to create post');
      }

      onSuccess?.();
    } catch (err) {
      console.error('Failed to create post:', err);
      setError(err.message ?? 'Failed to create post');
    } finally {
      isSubmittingRef.current = false;
      setIsSubmitting(false);
    }
  };

  return (
    <article className="post" id="form-container">

      <div id="post-header">
        <div id="op-info">
          <ProfileAvatar
            user={user ? { ...user, id: user.userId } : null}
            imageProps={{ id: 'op-avatar' }}
            placeholderProps={{ id: 'op-avatar-placeholder' }}
          />
          <div id="op-details">
            <p id="op-name">{user?.pseudo ?? 'You'}</p>
            <p id="datetime">Draft</p>
          </div>
        </div>
      </div>

      {/* Title below the nameplate */}
      <input
        id="form-title"
        placeholder="Title..."
        value={title}
        disabled={isSubmitting}
        onChange={e => setTitle(e.target.value)}
      />

      <div className="post-tags-input-wrap">
        <input
          id="form-tags"
          placeholder="Tags (comma-separated)"
          value={tags}
          disabled={isSubmitting}
          onChange={e => setTags(e.target.value)}
        />
      </div>

      <div id="marketplace-fields">
        <label id="marketplace-toggle">
          <input
            type="checkbox"
            checked={isMarketplaceItem}
            disabled={isSubmitting}
            onChange={(e) => {
              setIsMarketplaceItem(e.target.checked);
              if (!e.target.checked) setPrice("");
            }}
          />
          <span>Item for sale</span>
        </label>

        {isMarketplaceItem && (
          <input
            id="form-price"
            type="number"
            min="0"
            step="0.01"
            placeholder="Price"
            value={price}
            disabled={isSubmitting}
            onChange={(e) => setPrice(e.target.value)}
          />
        )}
      </div>

      <div
        id="image-drop-zone"
        className={dragging ? 'dragging' : ''}
        onClick={() => {
          if (!isSubmitting) fileInputRef.current?.click();
        }}
        onDragOver={e => {
          e.preventDefault();
          if (!isSubmitting) setDragging(true);
        }}
        onDragLeave={() => setDragging(false)}
        onDrop={handleDrop}
      >
        {imagePreviews.length > 0 ? (
          <div id="image-container">
            {imagePreviews.length > 1 && mediaIndex > 0 && (
              <button className="media-arrow media-arrow-left" type="button"
                onClick={e => { e.stopPropagation(); setMediaIndex(i => i - 1); }}>‹</button>
            )}
            <img id="image" src={imagePreviews[mediaIndex]} alt={`Preview ${mediaIndex + 1}`} />
            {imagePreviews.length > 1 && mediaIndex < imagePreviews.length - 1 && (
              <button className="media-arrow media-arrow-right" type="button"
                onClick={e => { e.stopPropagation(); setMediaIndex(i => i + 1); }}>›</button>
            )}
            {imagePreviews.length > 1 && (
              <div className="media-dots">
                {imagePreviews.map((_, i) => (
                  <span key={i} className={`media-dot ${i === mediaIndex ? 'active' : ''}`} />
                ))}
              </div>
            )}
          </div>
        ) : (
          <div id="drop-placeholder">
            <span>📷 Click or drag to add photos</span>
          </div>
        )}
        <input
          ref={fileInputRef}
          type="file"
          id="form-image"
          accept="image/*"
          multiple
          style={{ display: 'none' }}
          disabled={isSubmitting}
          onChange={e => handleImageFile(e.target.files)}
        />
      </div>

      <textarea
        id="form-text"
        placeholder="Write something..."
        rows={6}
        value={textContent}
        disabled={isSubmitting}
        onChange={e => setTextContent(e.target.value)}
      />

      {error && <p id="form-error">{error}</p>}

      <div className="reaction-bar">
        <button id="form-post" className="reaction-button" type="button" onClick={submit} disabled={isSubmitting}>
          {isSubmitting ? 'Posting...' : 'Post'}
        </button>
        <button id="form-cancel" className="reaction-button" type="button" onClick={onCancel} disabled={isSubmitting}>
          Cancel
        </button>
      </div>

    </article>
  );
}
