/** Style imports */
import { useState, useEffect } from 'react';
import '../.././assets/CreatePost.css'
import { getBaseUrl } from '../../API';

/** Create post popup */
export default function CreatePost({onRemove, onCreate}){
    /** Form variable use states */
    const [title, setTitle] = useState("");
    const [imageFile, setImageFile] = useState(null);
    const [imagePreview, setImagePreview] = useState("");
    const [tags, setTags] = useState("");
    const [textContent, setTextContent] = useState("");
    const [error, setError] = useState("");

    /** Form management
     * The submit tab manages the submission to the db (for later)
     */
    const submit = async (event) => {
        event.preventDefault();
        setError("");

        if (!title.trim() || !textContent.trim()) {
          setError('Title and text are required.');
          return;
        }

        try {
          const response = await fetch(`${getBaseUrl()}/post`, {
            method: 'POST',
            credentials: 'include',
            headers: {
              'Content-Type': 'application/json',
            },
            body: JSON.stringify({
              nom: title,
              contenu: textContent,
              tags: tags.split(',').map((tag) => tag.trim()).filter(Boolean),
            }),
          });

          if (!response.ok) {
            const message = await response.text();
            throw new Error(message || 'Failed to create post');
          }

          const createdPost = await response.json();
          const newFeedPost = {
            id: createdPost.id,
            title: createdPost.titre ?? title,
            contents: createdPost.contenu ?? textContent,
            op: 'You',
            datetime: createdPost.datePublication ?? new Date().toISOString(),
            imageUrl: imagePreview || createdPost.imageUrl || null,
            tags: createdPost.tags ?? tags.split(',').map((tag) => tag.trim()).filter(Boolean),
            likes: createdPost.likes ?? 0,
            dislikes: createdPost.dislikes ?? 0,
            comments: createdPost.comments ?? [],
          };

          onCreate?.(newFeedPost);
          onRemove();
        } catch (err) {
          console.error('Failed to create post:', err);

          const newFeedPost = {
            id: `local-${Date.now()}`,
            title,
            contents: textContent,
            op: 'You (offline)',
            datetime: new Date().toISOString(),
            imageUrl: imagePreview || null,
            tags: tags.split(',').map((tag) => tag.trim()).filter(Boolean),
            likes: 0,
            dislikes: 0,
            comments: [],
          };

          onCreate?.(newFeedPost);
          onRemove();
          setError('Backend post fail; added locally to feed only.');
        }
    };

    const handleTitle = (event) => {setTitle(event.target.value);};
    const handleImageFile = (event) => {
      const file = event.target.files?.[0] ?? null;
      setImageFile(file);
      if (file) {
        const url = URL.createObjectURL(file);
        setImagePreview(url);
      } else {
        setImagePreview("");
      }
    };
    const handleTextContent = (event) => {setTextContent(event.target.value);};
    const handleTags = (event) => {setTags(event.target.value);};

    useEffect(() => {
      return () => {
        if (imagePreview) {
          URL.revokeObjectURL(imagePreview);
        }
      };
    }, [imagePreview]);

  return(
    <article className="post" id="form-container">
      <div id="post-header">
        <h2 id="title">New post</h2>
        <div id="op-info">
          <p id="op-name">Draft</p>
          <p id="datetime">Create a new post</p>
        </div>
      </div>

      <form id="create-post-form" onSubmit={submit}>
        {/** Post details */}
        <input id="form-title" placeholder="Title" value={title} onChange={handleTitle} />
        <input
          id="form-image"
          type="file"
          accept="image/*"
          onChange={handleImageFile}
        />
        <input
          id="form-tags"
          placeholder="Tags (comma-separated)"
          value={tags}
          onChange={handleTags}
        />
        {imagePreview && (
          <div id="image-preview-container">
            <img id="image-preview" src={imagePreview} alt="Selected post preview" />
          </div>
        )}
        <textarea
          id="form-text"
          placeholder="Add text"
          rows={12}
          value={textContent}
          onChange={handleTextContent}
        />

        {error && <p id="form-error">{error}</p>}

        {/** Confirmation buttons
         * In either case the popup closes once you press them
         * If the form does not have a title or text at minimum the post submission fails
        */}
        <div id="form-actions">
          <button id="form-post" type="submit">
            <p id="form-post-text">Post</p>
          </button>
          <button id="form-cancel" type="button" onClick={onRemove}>
            <p id="form-cancel-text">Cancel</p>
          </button>
        </div>
      </form>
    </article>
  );
}