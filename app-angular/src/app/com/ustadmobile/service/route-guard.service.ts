import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router, Route } from '@angular/router';
import { UmBaseService } from './um-base.service';
import { Injectable } from '@angular/core';
import { UmAngularUtil } from '../util/UmAngularUtil';

@Injectable({
  providedIn: 'root'
})
export class RouteGuardService implements CanActivate {
  constructor(private _router: Router, private umBaseService: UmBaseService) {}
  
  canActivate(next: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
   return localStorage.getItem(UmAngularUtil.DISPATCH_RESOURCE) === "true";
  }

}
