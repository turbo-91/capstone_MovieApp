import Layout from "./components/layout/Layout.tsx";
import {Route, Routes} from "react-router-dom";
import MoviesOfTheDay from "./components/MoviesOfTheDay.tsx";
import ProtectedRoute from "./components/ProtectedRoute.tsx";
import {useState} from "react";


function App() {

    const [user, setUser] = useState<string | undefined>();

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
