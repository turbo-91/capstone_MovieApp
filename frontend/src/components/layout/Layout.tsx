import Header from "./Header.tsx";
import { ReactNode } from "react";
import styled from "styled-components";

interface LayoutProps {
    children: ReactNode;
    user: string | undefined;
    setUser: (user: string | undefined) => void;
}

// Layout Container - Ensures full-screen black background
const LayoutContainer = styled.div`
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: flex-start;
    min-height: 100vh;
    width: 100vw;
    background-color: black;
    color: white;
    font-family: Helvetica, Arial, sans-serif;
`;

// Consistent width for Header and Main Content
const ContentWrapper = styled.div`
    width: 80vw;  // Ensures same width for both Header and MainContent
    max-width: 1400px;
    margin: auto;
`;

// Main Content Styling
const MainContent = styled.main`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
`;

const Layout: React.FC<LayoutProps> = ({ children, user, setUser }) => {
    return (
        <LayoutContainer>
            <ContentWrapper>
                <Header user={user} setUser={setUser} />
            </ContentWrapper>
            <ContentWrapper>
                <MainContent>{children}</MainContent>
            </ContentWrapper>
        </LayoutContainer>
    );
};

export default Layout;
