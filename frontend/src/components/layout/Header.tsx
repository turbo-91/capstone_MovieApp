import {useEffect, useState} from "react";
import NavBar from "./NavBar";
import axios from 'axios'
import {User} from "../../types/User.ts";

type HeaderProps = {
    user: string | undefined;
    setUser: (user: string | undefined) => void;
}

export default function Header(props: HeaderProps) {
    const {user, setUser} = props;
    const [menu, setMenu] = useState<boolean>(false);

    function login() {
        const host = window.location.host === 'localhost:5173' ? 'http://localhost:8080' : window.location.origin;
        window.open(`${host}/oauth2/authorization/github`, '_self');
    }

    function logout() {
        const host = window.location.host === 'localhost:5173' ? 'http://localhost:8080' : window.location.origin;

        window.open(`${host}/api/users/logout`, '_self'); // Correct logout URL
    }

    const loadUser = () => {
        axios.get('/api/users/active')
            .then(response => {
                setUser(response.data)
            })
            .catch(error => {
                setUser(undefined)
            })
    }

    useEffect(() => {
        loadUser()
    }, [])



    return (
        <div>
            <button onClick={login}>login</button>
            <button onClick={logout}>logout</button>
            <p>{user}</p>
            <h1>MovieApp</h1>
            {user && (
                <>
                    <h1 onClick={() => setMenu((prevMenu) => !prevMenu)}>Menu</h1>
                    {menu && <NavBar />}
                </>
            )}
        </div>
    );
}
