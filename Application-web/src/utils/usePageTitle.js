import { useEffect } from "react";

const usePageTitle = (pageTitle) => {
    useEffect(() => {
        document.title = pageTitle ? `VillagETS - ${pageTitle}` : "VillagETS";
    }, [pageTitle]);
};

export default usePageTitle;