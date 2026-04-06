/** DO NOT TOUCH */

export const getBaseUrl = () => {
  if (window.location.hostname === 'localhost') {
    return 'http://localhost:5173';
  }
  return 'https://apivillagets.lesageserver.com';
};