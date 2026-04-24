import { useEffect, useRef, useState } from 'react';

const GIPHY_API_KEY = import.meta.env.VITE_GIPHY_API_KEY;

export default function GiphyPicker({ onSelect, onClose }) {
    const [query, setQuery] = useState('');
    const [gifs, setGifs] = useState([]);
    const [loading, setLoading] = useState(false);
    const debounceRef = useRef(null);
    const isMounted = useRef(false);

    const fetchGifs = async (search) => {
        setLoading(true);
        const endpoint = search
            ? `https://api.giphy.com/v1/gifs/search?api_key=${GIPHY_API_KEY}&q=${encodeURIComponent(search)}&limit=18&rating=g`
            : `https://api.giphy.com/v1/gifs/trending?api_key=${GIPHY_API_KEY}&limit=18&rating=g`;

        try {
            const res = await fetch(endpoint);
            const data = await res.json();
            setGifs(data.data ?? []);
        } catch (err) {
            console.error('Giphy fetch failed:', err);
            setGifs([]);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchGifs('');
    }, []);

    useEffect(() => {
        if (!isMounted.current) {
            isMounted.current = true;
            return;
        }

        clearTimeout(debounceRef.current);
        debounceRef.current = setTimeout(() => fetchGifs(query), 400);

        return () => clearTimeout(debounceRef.current);
    }, [query]);

    return (
        <div className="giphy-picker">
            <div className="giphy-header">
                <input
                    autoFocus
                    placeholder="Rechercher un GIF..."
                    value={query}
                    onChange={(event) => setQuery(event.target.value)}
                    className="giphy-search"
                />
                <button type="button" onClick={onClose} className="giphy-close">
                    &times;
                </button>
            </div>

            <div className="giphy-grid">
                {loading && <p className="giphy-loading">Chargement...</p>}
                {!loading && gifs.map((gif) => (
                    <img
                        key={gif.id}
                        src={gif.images.fixed_height_small.url}
                        alt={gif.title}
                        className="giphy-item"
                        onClick={() => onSelect(gif.images.fixed_height_small.url)}
                    />
                ))}
            </div>

            <p className="giphy-attribution">Powered by GIPHY</p>
        </div>
    );
}
