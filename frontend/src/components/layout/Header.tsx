import { useState } from "react";
import NavBar from "./NavBar";

export default function Header() {
    const [menu, setMenu] = useState<boolean>(false);

    return (
        <div>
            <h1>MovieApp</h1>
            <h1 onClick={() => setMenu((prevMenu) => !prevMenu)}>Menu</h1>
            {menu && <NavBar />}
        </div>
    );
}
