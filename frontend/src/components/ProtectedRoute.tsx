import {Navigate, Outlet} from "react-router-dom";

type ProtectedRouteProps = {
    user: string | undefined
}

export default function ProtectedRoute({ user }: ProtectedRouteProps){
    if (user === undefined) {
        return <div>Loading...</div>;
    }

    const isAuthenticated = !!user && user !== "anonymousUser";
    console.log("user in protectedRoute", user)

    return (
        isAuthenticated ? <Outlet /> : <Navigate to={"/"} />
    )

}