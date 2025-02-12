import Header from "./Header.tsx";
import {ReactNode} from "react";

interface LayoutProps {
    children: ReactNode;
}


const Layout: React.FC<LayoutProps> = ({ children }) => {
    return (
        <div className="layout">
            <Header />
            <main>{children}</main>
        </div>
    );
};

export default Layout;
