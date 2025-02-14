import {useEffect, useState} from "react";
import NavBar from "./NavBar";
import axios from 'axios'
import {User} from "../../types/User.ts";

type HeaderProps = {
    user: User | undefined;
    setUser: (user: User | undefined) => void;
}

export default function Header(props: HeaderProps) {
    const {user, setUser} = props;
    const [menu, setMenu] = useState<boolean>(false);


function logout() {
    axios.post("api/users/logout")
}

    return (
        <div>
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
