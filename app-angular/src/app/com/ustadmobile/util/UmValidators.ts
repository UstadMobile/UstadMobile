import { FormControl } from '@angular/forms';

export function umValidatePassword(control: FormControl): {[s: string]: boolean} {
    return /^([0-9]+[a-zA-Z]+|[a-zA-Z]+[0-9]+)[0-9a-zA-Z]*$/.test(control.value)
    ? {validPassword: true} : {validPassword: false};
}
