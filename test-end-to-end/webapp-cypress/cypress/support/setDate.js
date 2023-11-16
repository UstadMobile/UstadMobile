
//https://github.com/cypress-io/cypress/issues/1366#issuecomment-437878862
export default (input, value) => {
  const nativeInputValueSetter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;
  nativeInputValueSetter.call(input, value);

  const event = new Event('input', { bubbles: true });
  input.dispatchEvent(event);
};