import Layout from "./components/layout/Layout.tsx";
import {Route, Routes} from "react-router-dom";
import MoviesOfTheDay from "./components/MoviesOfTheDay.tsx";
import ProtectedRoute from "./components/ProtectedRoute.tsx";
import {useState} from "react";
import axios from "axios";
import SearchQuery from "./components/SearchQuery.tsx";
import Watchlist from "./components/Watchlist.tsx";


function App() {

    const [user, setUser] = useState<string | undefined>();
    axios.defaults.baseURL = window.location.host === 'localhost:5173'
        ? 'http://localhost:8080'
        : window.location.origin;

    axios.defaults.withCredentials = true;
    console.log("user in App", user)

    return (
            <Layout user={user} setUser={setUser}>
                <main>
                    <Routes>
                        <Route
                            path="/"
                            element={<MoviesOfTheDay user={user}/>}

                        />
                        <Route element={<ProtectedRoute user={user}/>}>
                            <Route path="/search" element={<SearchQuery user={user} />} />
                            <Route path="/watchlist" element={<Watchlist user={user} />} />
                        </Route>
                    </Routes>
                </main>
            </Layout>
    );
}

export default App;
