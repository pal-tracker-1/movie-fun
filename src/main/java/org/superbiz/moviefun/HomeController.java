package org.superbiz.moviefun;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.superbiz.moviefun.albums.Album;
import org.superbiz.moviefun.albums.AlbumFixtures;
import org.superbiz.moviefun.albums.AlbumsBean;
import org.superbiz.moviefun.movies.Movie;
import org.superbiz.moviefun.movies.MovieFixtures;
import org.superbiz.moviefun.movies.MoviesBean;

import java.util.Map;

@Controller
public class HomeController {

    private final MoviesBean moviesBean;
    private final AlbumsBean albumsBean;
    private final MovieFixtures movieFixtures;
    private final AlbumFixtures albumFixtures;
    private final PlatformTransactionManager albumsPlatformTransactionManager;
    private final PlatformTransactionManager moviesPlatformTransactionManager;


    public HomeController(MoviesBean moviesBean, AlbumsBean albumsBean, MovieFixtures movieFixtures, AlbumFixtures albumFixtures, PlatformTransactionManager albumsPlatformTransactionManager, PlatformTransactionManager moviesPlatformTransactionManager) {
        this.moviesBean = moviesBean;
        this.albumsBean = albumsBean;
        this.movieFixtures = movieFixtures;
        this.albumFixtures = albumFixtures;
        this.albumsPlatformTransactionManager = albumsPlatformTransactionManager;
        this.moviesPlatformTransactionManager = moviesPlatformTransactionManager;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/setup")
    public String setup(Map<String, Object> model) {

        TransactionTemplate movieTemplate =
                new TransactionTemplate(this.moviesPlatformTransactionManager);

        movieTemplate.execute(transactionStatus -> {
            for (Movie movie : movieFixtures.load()) {
                moviesBean.addMovie(movie);
            }
            return null;
        });

        TransactionTemplate albumTemplate =
                new TransactionTemplate(this.albumsPlatformTransactionManager);


        albumTemplate.execute(transactionStatus -> {
            for (Album album : albumFixtures.load()) {
                albumsBean.addAlbum(album);
            }
            return null;
        });

        model.put("movies", moviesBean.getMovies());
        model.put("albums", albumsBean.getAlbums());

        return "setup";
    }
}
