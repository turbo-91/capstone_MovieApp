import Layout from "./components/layout/Layout.tsx";
import {Route, Routes} from "react-router-dom";
import MoviesOfTheDay from "./components/MoviesOfTheDay.tsx";
import ProtectedRoute from "./components/ProtectedRoute.tsx";
import {useState} from "react";
import axios from "axios";


function App() {

    const [user, setUser] = useState<string | undefined>();
    axios.defaults.baseURL = window.location.host === 'localhost:5173'
        ? 'http://localhost:8080'
        : window.location.origin;

    axios.defaults.withCredentials = true;

    return (
            <Layout user={user} setUser={setUser}>
                <main>
                    <Routes>
                        <Route
                            path="/"
                            element={<MoviesOfTheDay/>}

                        />
                        <Route element={<ProtectedRoute user={user}/>}>

                        </Route>
                    </Routes>
                </main>
            </Layout>
    );
}

export default App;
