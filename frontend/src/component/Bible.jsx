import { useEffect, useState } from "react";

function Bible() {
    const [bible, setBible] = useState([]);

    useEffect(() => {
        fetch("http://localhost:8081/api/external/bibles")
        .then((res) => {
            if (!res.ok) throw new Error("error: " + res.status);
            return res.json();
        })
        .then((data) => {
            console.log("Fetched bible data:", data);
            setBible(data);
        })
        .catch((e) => console.error("Unable to fetch bible data", e));
    }, []);

    return (
        <div>
            <h1>Bible data</h1>

            <div>
                {bible.length > 0 ? (
                    <ul>
                        {bible.map((item, index) => (
                            <li key={index}>
                                <strong>{item.bible_id}</strong> - {item.language} - {item.version}
                            </li>
                        ))}
                    </ul>
                ) : (
                    <p>Loading bible data...</p>
                )}
            </div>
        </div>
    )
}

export default Bible;