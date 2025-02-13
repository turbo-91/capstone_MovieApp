import Header from "./Header.tsx";
import {ReactNode} from "react";

interface LayoutProps {
    children: ReactNode;
    user: string | undefined;
    setUser: (user: string | undefined) => void;
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
