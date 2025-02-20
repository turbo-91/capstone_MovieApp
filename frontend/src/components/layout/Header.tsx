import { useEffect, useState } from "react";
import NavBar from "./NavBar";
import axios from "axios";
import styled from "styled-components";

type HeaderProps = {
    user: string | undefined;
    setUser: (user: string | undefined) => void;
};

// Styled Components
const HeaderContainer = styled.header`
    display: flex;
    justify-content: space-between;
    align-items: center;
    background-color: black;
    padding: 1rem 2rem;
    color: white;
    font-family: Helvetica, Arial, sans-serif;
    width: 100%;
`;

const Title = styled.h1`
    margin: 0;
    font-size: 1.8rem;
    font-weight: bold;
    cursor: pointer;
`;

const UserSection = styled.div`
    display: flex;
    align-items: center;
    gap: 1rem;

    button {
        background: none;
        border: 1px solid white;
        color: white;
        padding: 0.5rem 1rem;
        cursor: pointer;
        font-size: 1rem;
        transition: 0.3s ease-in-out;

        &:hover {
            background: white;
            color: black;
        }
    }

    p {
        margin: 0;
    }
`;

const MenuButton = styled.h1`
  cursor: pointer;
  font-size: 1.2rem;
  transition: color 0.3s ease-in-out;

  &:hover {
    color: gray;
  }
`;

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
        <HeaderContainer>
            <Title>MovieApp</Title>

            <UserSection>
                <button onClick={user && user !== "Unauthorized" ? logout : login}>
                    {user && user !== "Unauthorized" ? "Logout" : "Login"}
                </button>
                <p>{user}</p>
                {user && user !== "Unauthorized" && (
                    <>
                        <MenuButton onClick={() => setMenu((prev) => !prev)}>Menu</MenuButton>
                        {menu && <NavBar />}
                    </>
                )}
            </UserSection>
        </HeaderContainer>
    );
}
