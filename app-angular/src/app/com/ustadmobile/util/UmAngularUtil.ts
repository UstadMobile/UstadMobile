import { ActivatedRoute, Params } from '@angular/router';
export class UmAngularUtil{

    static paramsToMap(params: Params){
        
        params.forEach((value: string, key: string) => {
            const param = {key: value};
            
        });
    }

    static mapToParams(map: Params){
        const urlParams = {};
        map.forEach((value: string, key: string) => {
            const param = {key: value};
            
        });
    }
}