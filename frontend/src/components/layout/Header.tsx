import { useEffect, useState } from "react";
import NavBar from "./NavBar";
import axios from "axios";

type HeaderProps = {
    user: string | undefined;
    setUser: (user: string | undefined) => void;
};

export default function Header({ user, setUser }: HeaderProps) {
    const [menu, setMenu] = useState<boolean>(false);

    function login() {
        const host =
            window.location.host === "localhost:5173"
                ? "http://localhost:8080"
                : window.location.origin;
        window.open(`${host}/oauth2/authorization/github`, "_self");
    }

    function logout() {
        const host =
            window.location.host === "localhost:5173"
                ? "http://localhost:8080"
                : window.location.origin;
        window.open(`${host}/api/users/logout`, "_self");
    }

    const loadUser = () => {
        axios
            .get("/api/users/active")
            .then((response) => {
                const loggedInUser = response.data;
                console.log("User successfully loaded:", loggedInUser);
                setUser(loggedInUser);
                axios
                    .post(`/api/users/save/${loggedInUser}`)
                    .then(() => console.log("User successfully saved in backend"))
                    .catch((error) => console.error("Error saving user:", error));
            })
            .catch((error) => {
                console.log("Error loading user:", error);
                setUser(undefined);
            });
    };

    useEffect(() => {
        loadUser();
    }, []);

    return (
        <div>
            <button onClick={user && user !== "Unauthorized" ? logout : login}>
                {user && user !== "Unauthorized" ? "Logout" : "Login"}
            </button>
            <p>{user}</p>
            <h1>MovieApp</h1>
            {user && user !== "Unauthorized" && (
                <>
                    <h1 onClick={() => setMenu((prev) => !prev)}>Menu</h1>
                    {menu && <NavBar />}
                </>
            )}
        </div>
    );
}
