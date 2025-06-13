import axios from 'axios';

axios.defaults.xsrfCookieName = 'NO_XSRF_TOKEN';

export default axios;