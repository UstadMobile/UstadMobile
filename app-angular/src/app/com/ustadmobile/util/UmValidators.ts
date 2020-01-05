import { FormControl } from '@angular/forms';

/**
 * Validate user password before submitting form
 * @param control form controls
 */
export function umValidatePassword(control: FormControl): {[s: string]: boolean} {
    return /^([0-9]+[a-zA-Z]+|[a-zA-Z]+[0-9]+)[0-9a-zA-Z]*$/.test(control.value)
    ? {validPassword: true} : {validPassword: false};
}
