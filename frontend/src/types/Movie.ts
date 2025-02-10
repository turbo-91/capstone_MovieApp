export interface IMovie {
    netzkinoId: number;
    slug: string;
    title: string;
    year: string;
    overview: string;
    regisseur: string;
    stars: string;
    imgNetzkino: string;
    imgNetzkinoSmall: string;
    imgImdb: string;
    queries: string[];
    dateFetched?: string[];
}
