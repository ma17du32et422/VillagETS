/** DO NOT TOUCH */

export const getBaseUrl = () => {
  if (window.location.hostname === 'localhost') {
    return 'http://localhost:5000';
  }
  return 'https://apivillagets.lesageserveur.com';
};