import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import "bootstrap/dist/css/bootstrap.min.css";
import "./HomePage.css"; // your custom CSS

function HomePage() {
  const [books, setBooks] = useState([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(3);
  const [sort, setSort] = useState("id,asc");

  useEffect(() => {
    const params = new URLSearchParams({
      page: String(page),
      size: String(size),
      sort,
    });

    /*async function fetchBooks() {
      try {
        const data = await fetch(`http://localhost:8081/api/books?${params.toString()}`);
        const fetchData = await data.json();
        setBooks(fetchData.content);
        setTotalElements(fetchData.totalElements);
        setTotalPages(fetchData.totalPages);
        // console.log("Fetched books:", fetchData);
        const sorted = fetchData.sort(
          (a, b) => new Date(b.createdAt) - new Date(a.createdAt)
        );

        // Limit to max 10 books
        // setBooks(sorted.slice(0, 10));
      } catch (e) {
        console.error("Unable to fetch", e);
      }
    } 
    fetchBooks(); */
       
    fetch("http://localhost:8081/api/books?" + params.toString())
      .then((res) => {
        if (!res.ok) throw new Error("error: " + res.status);
        return res.json();
      })
      .then((data) => {
        const content = Array.isArray(data) ? data : (data?.content || []);
        setBooks(content);
        setTotalElements(Array.isArray(data) ? content.length : (data?.totalElements || 0));
        setTotalPages(Array.isArray(data) ? 1 : (data?.totalPages || 1));
        // console.log("Fetched books:", data);
      })
      .catch((e) => console.error("Unable to fetch books", e));
  }, [page, size, sort]);

  const sizeHandler = (newSize) => {
    setSize(newSize);
    setPage(0);
  };

  const sortHandler = (newSort) => {
    setSort(newSort);
    setPage(0);
  };

  return (
    <div
      className="container mt-4"
      style={{
        minHeight: "100vh",
        padding: "20px",
      }}
    > 

      <div>
        {page* size + 1}-{(page + 1) * size > totalElements ? totalElements : (page + 1) * size}
        kuvatud {totalElements}-st
      </div>

      <select defaultValue={3} onChange={(e) => sizeHandler(Number(e.target.value))}>
        <option>2</option>
        <option>3</option>
        <option>4</option>
      </select>

      <br /><br />

      <button onClick={() => sortHandler("id,asc")}>Sorteeri id,asc</button>
      <button onClick={() => sortHandler("id,desc")}>Sorteeri id,desc</button>

      <br /><br />

      <button disabled={page === 0} onClick={() => setPage(page - 1)}>Eelmine</button>
      <span>{page + 1}</span>
      <button disabled={page+1 === totalPages} onClick={() => setPage(page + 1)}>Järgmine</button>

      <h2
        className="text-center mb-4"
        style={{ color: "#000", textShadow: "1px 1px 2px #f5f5f5ff" }}
      >
        Latest Books
      </h2>

      {books.length === 0 ? (
        <p className="text-center" style={{ color: "#555" }}>
          No books available.
        </p>
      ) : (
        <div
          className="d-flex gap-3 overflow-auto"
          style={{
            width: "100%",
            paddingBottom: "10px",
            // Show only 5 cards in viewport
            scrollSnapType: "x mandatory",
          }}
        >
          {books.map((book) => (
            <Link
              to={`/books/${book.id}`}
              key={book.id}
              style={{
                textDecoration: "none",
                flex: "0 0 20%", // 5 cards per view
                scrollSnapAlign: "start",
              }}
            >
              <div className="book-card p-2 shadow-sm" style={{ minWidth: "200px", height: "400px" }}>
                {/* Book Image */}
                {book.image ? (
                  <img
                    src={book.image ? `http://localhost:8081${book.image}` : "/no-image.png"}
                    alt={book.title}
                    style={{ width: "100%", height: "250px", objectFit: "cover" }}
                  />
                ) : (
                  <div
                    style={{
                      height: "250px",
                      backgroundColor: "#eee",
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                      marginBottom: "8px",
                      fontSize: "0.9rem",
                      color: "#888",
                    }}
                  >
                    No Image
                  </div>
                )}

                {/* Title */}
                <h5 style={{ color: "#333", minHeight: "2.5rem" }}>{book.title}</h5>

                {/* Description */}
                <p style={{ color: "#555", fontSize: "0.85rem", minHeight: "3rem" }}>
                  {book.description || "No description available"}
                </p>

                {/* Shelf */}
                <p style={{ color: "#000000ff", fontSize: "0.8rem", fontWeight: "bold"}}>
                  Shelf: {book.shelf || "N/A"}
                </p>

              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}

export default HomePage;
