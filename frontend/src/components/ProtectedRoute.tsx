import {Navigate, Outlet} from "react-router-dom";
import {User} from "../types/User.ts";

type ProtectedRouteProps = {
    user: User | undefined
}

export default function ProtectedRoute(props: ProtectedRouteProps){
    const isAuthenticated = props.user?.githubId != undefined && props.user?.githubId != "anonymousUser"

    return (
        isAuthenticated ? <Outlet /> : <Navigate to={"/"} />
    )

}