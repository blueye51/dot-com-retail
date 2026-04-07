import {useEffect, useRef, useState, useCallback} from "react";

const API_KEY = import.meta.env.VITE_GOOGLE_MAPS_API_KEY;

export default function AddressAutocomplete({onAddressSelect, className}) {
    const inputRef = useRef(null);
    const dropdownRef = useRef(null);
    const debounceRef = useRef(null);
    const [suggestions, setSuggestions] = useState([]);
    const [show, setShow] = useState(false);

    const fetchSuggestions = useCallback(async (input) => {
        if (input.length < 3) { setSuggestions([]); setShow(false); return; }
        try {
            const res = await fetch("https://places.googleapis.com/v1/places:autocomplete", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "X-Goog-Api-Key": API_KEY,
                },
                body: JSON.stringify({input, includedPrimaryTypes: ["street_address", "premise", "subpremise"]}),
            });
            const data = await res.json();
            setSuggestions(data.suggestions?.filter(s => s.placePrediction) || []);
            setShow(true);
        } catch {
            setSuggestions([]);
        }
    }, []);

    const handleSelect = useCallback(async (suggestion) => {
        const placeId = suggestion.placePrediction.placeId;
        setShow(false);
        if (inputRef.current) {
            inputRef.current.value = suggestion.placePrediction.text.text;
        }
        try {
            const res = await fetch(`https://places.googleapis.com/v1/places/${placeId}`, {
                headers: {
                    "X-Goog-Api-Key": API_KEY,
                    "X-Goog-FieldMask": "addressComponents,formattedAddress",
                },
            });
            const place = await res.json();
            if (!place.addressComponents) return;

            const get = (type) => {
                const comp = place.addressComponents.find((c) => c.types.includes(type));
                return comp ? comp.longText : "";
            };
            const getShort = (type) => {
                const comp = place.addressComponents.find((c) => c.types.includes(type));
                return comp ? comp.shortText : "";
            };

            const streetAddress = `${get("street_number")} ${get("route")}`.trim();
            // Fallback chain: street_number+route → premise → subpremise → first part of formatted address
            const city = get("locality") || get("sublocality_level_1") || get("administrative_area_level_2");
            let address = streetAddress || get("premise") || get("subpremise");
            if (!address && place.formattedAddress) {
                const parts = place.formattedAddress.split(",");
                address = parts[0]?.trim() || "";
            }

            onAddressSelect({
                address,
                city,
                state: getShort("administrative_area_level_1"),
                zip: get("postal_code"),
                country: getShort("country"),
            });
        } catch { /* ignore */ }
    }, [onAddressSelect]);

    const handleInput = useCallback((e) => {
        clearTimeout(debounceRef.current);
        debounceRef.current = setTimeout(() => fetchSuggestions(e.target.value), 300);
    }, [fetchSuggestions]);

    useEffect(() => {
        const handleClickOutside = (e) => {
            if (dropdownRef.current && !dropdownRef.current.contains(e.target) && e.target !== inputRef.current) {
                setShow(false);
            }
        };
        document.addEventListener("mousedown", handleClickOutside);
        return () => {
            document.removeEventListener("mousedown", handleClickOutside);
            clearTimeout(debounceRef.current);
        };
    }, []);

    return (
        <div style={{position: "relative"}}>
            <input
                ref={inputRef}
                type="text"
                placeholder="Start typing your address..."
                className={className}
                autoComplete="one-time-code"
                onInput={handleInput}
                onFocus={() => { if (suggestions.length > 0) setShow(true); }}
            />
            {show && suggestions.length > 0 && (
                <div
                    ref={dropdownRef}
                    style={{
                        position: "absolute", zIndex: 1000, background: "#1e1e1e",
                        border: "1px solid #444", borderRadius: "4px", maxHeight: "200px",
                        overflowY: "auto", width: "100%", marginTop: "2px",
                    }}
                >
                    {suggestions.map((s, i) => (
                        <div
                            key={i}
                            style={{padding: "8px 12px", cursor: "pointer", color: "#e0e0e0", fontSize: "0.9rem"}}
                            onMouseEnter={(e) => e.currentTarget.style.background = "#333"}
                            onMouseLeave={(e) => e.currentTarget.style.background = "transparent"}
                            onMouseDown={(e) => { e.preventDefault(); handleSelect(s); }}
                        >
                            {s.placePrediction.text.text}
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}
