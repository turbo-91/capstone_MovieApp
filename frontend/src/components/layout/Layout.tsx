import Header from "./Header.tsx";
import {ReactNode} from "react";
import {User} from "../../types/User.ts";

interface LayoutProps {
    children: ReactNode;
    user: User | undefined;
    setUser: (user: User | undefined) => void;
}


const Layout: React.FC<LayoutProps> = ({ children, user, setUser }) => {
    return (
        <div className="layout">
            <Header user={user} setUser={setUser}/>
            <main>{children}</main>
        </div>
    );
};

export default Layout;
