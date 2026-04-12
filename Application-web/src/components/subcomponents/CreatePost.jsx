/** Style imports */
import { useState, useEffect } from 'react';
import '../.././assets/CreatePost.css'
import { getBaseUrl } from '../../API';

/** Create post popup */
export default function CreatePost({ onRemove, onCreate, user }) {
    /** Form variable use states */
    const [title, setTitle] = useState("");
    const [imageFiles, setImageFiles] = useState([]);
    const [imagePreviews, setImagePreviews] = useState([]);
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
            headers: {
              'Content-Type': 'application/json',
            },
            body: JSON.stringify({
              nom: title,
              contenu: textContent,
              media: mediaUrls,
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
            op: {
              id: user?.userId,
              pseudo: user?.pseudo,
              photoProfil: user?.photoProfil,
            },
            datetime: createdPost.datePublication ?? new Date().toISOString(),
            media: createdPost.media ?? mediaUrls,
            tags: tags.split(',').map(t => t.trim()).filter(Boolean),
            likes: 0,
            dislikes: 0,
            comments: [],
          };

          onCreate?.(newFeedPost);
          onRemove();
        } catch (err) {
          console.error('Failed to create post:', err);
          setError(err.message ?? 'Failed to create post');
        }
    };

  const handleTitle = (event) => {setTitle(event.target.value);};
  const handleImageFile = (event) => {
    const files = Array.from(event.target.files ?? []);
    setImageFiles(files);
    setImagePreviews(files.map(f => URL.createObjectURL(f)));
  };
  const handleTextContent = (event) => {setTextContent(event.target.value);};
  const handleTags = (event) => {setTags(event.target.value);};

  useEffect(() => {
    return () => imagePreviews.forEach(url => URL.revokeObjectURL(url));
  }, [imagePreviews]);

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
          multiple
          onChange={handleImageFile}
        />
        <input
          id="form-tags"
          placeholder="Tags (comma-separated)"
          value={tags}
          onChange={handleTags}
        />
        {imagePreviews.map((src, i) => (
          <img key={i} id="image-preview" src={src} alt={`Preview ${i + 1}`} />
        ))}
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