import {Navigate, Outlet} from "react-router-dom";

type ProtectedRouteProps = {
    user: string | undefined
}

export default function ProtectedRoute({ user }: ProtectedRouteProps){
    const isAuthenticated = !!user && user !== "anonymousUser";

    return (
        isAuthenticated ? <Outlet /> : <Navigate to={"/"} />
    )

}