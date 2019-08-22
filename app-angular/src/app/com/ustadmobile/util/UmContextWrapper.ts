/**
 * Context wrapper which will be used to wrap router as our application context
 */

export class UmContextWrapper {
    private activeRoute : any;

    constructor(private router: any){}

    /**
     * Get active router object
     */
    getRouter(): any{
        return this.router;
    }

    /**
     * Setting active route
     * @param activeRoute current active route
     */
    setActiveRoute(activeRoute: any){
        this.activeRoute = activeRoute;
    }

    /**
     * Get current active route
     */
    getActiveRoute(): any {
        return this.activeRoute;
    }
}