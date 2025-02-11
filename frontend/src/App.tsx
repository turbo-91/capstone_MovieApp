import Layout from "./components/layout/Layout.tsx";
import {Route, Routes} from "react-router-dom";
import MoviesOfTheDay from "./components/MoviesOfTheDay.tsx";

function App() {


    return (
            <Layout>
                <main>
                    <Routes>
                        <Route
                            path="/"
                            element={<MoviesOfTheDay/>}

                        />
                    </Routes>
                </main>
            </Layout>
    );
}

export default App;
