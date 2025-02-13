import {useEffect, useState} from "react";
import NavBar from "./NavBar";
import axios from 'axios'

type HeaderProps = {
    user: string | undefined;
    setUser: (user: string | undefined) => void;
}

export default function Header(props: HeaderProps) {
    const {user, setUser} = props;
    const [menu, setMenu] = useState<boolean>(false);

    function login() {
        const host = window.location.host === 'localhost:5173' ? 'http://localhost:8080': window.location.origin
        window.open(host + '/oauth2/authorization/github', '_self')

    }

    useEffect(() => {
        getUserAxios(); // Update user state when component loads
    }, []);

    function getUserAxios() {
        axios.get("api/users/me").then((response) => {
            console.log("axios ", response.data);
            setUser(response.data);
        }).catch(() => {
            setUser(undefined); // Ensure user state is cleared if request fails
        });
    }

    function logout() {
        axios.post("api/users/logout").then(() => {
            setUser(undefined); // Clear user state on logout
        });
    }


    return (
        <div>
            <h1>MovieApp</h1>
            {user ? (
                <button onClick={logout}>Logout</button>
            ) : (
                <button onClick={login}>Login</button>
            )}
            <h1 onClick={() => setMenu((prevMenu) => !prevMenu)}>Menu</h1>
            {menu && <NavBar />}
        </div>
    );
}
