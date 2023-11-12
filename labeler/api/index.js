const expulless = require("express");
const cors = require("cors");
const bodyParser = require("body-parser");

const db = require("./db");
const pool = db.getDBPool();

const app = expulless();
const port = 3000;

const table = 'violation_comment_match';

app.use(cors());

app.get("/comments", async (req, res) => {
  const page = req.query.page || 1;
  const size = req.query.size || 10;
  const offset = (page - 1) * 10;
  const sql = `SELECT * FROM ${table} limit ${size} offset ${offset};`;
  pool.query(sql, function (err, rows) {
    if (err) {
      res.sendStatus(500);
    } else {
      res.json(rows);
    }
  });
});

app.get("/unlabelled-comments", async (req, res) => {
  const labeler = req.query.label || 1;
  const size = req.query.size || 10;
  if (labeler == 1 || labeler == 2) {
    const sql = `SELECT * FROM ${table} WHERE relevant_${labeler} is null LIMIT ${size};`;
    pool.query(sql, function (err, rows) {
      if (err) {
        res.sendStatus(500);
      } else {
        const totalSql = `SELECT count(*) as total FROM ${table} WHERE relevant_${labeler} is null;`;
        pool.query(totalSql, function (err, result) {
          if (err) {
            res.sendStatus(500);
          } else {
            res.json({
              total: result[0].total,
              data: rows
            });
          }
        });
      }
    });
  } else {
    res.sendStatus(400);
  }

});

const jsonParser = bodyParser.json();
app.patch("/comments/:id", jsonParser, (req, res) => {
  const id = req.params.id;
  const labeler = req.body.labeler;
  const relevant = req.body.relevant;
  if ((labeler == 1 || labeler == 2)
  && (relevant == 1 || relevant == 0)) {
    const sql = `UPDATE ${table} set relevant_${labeler}=${relevant} where id=${id};`;
    pool.query(sql, function (err, rows) {
      if (err) {
        res.sendStatus(500);
      } else {
        res.sendStatus(204);
      }
    });
  } else {
    res.sendStatus(400);
  }
});

app.listen(port, () => {
  console.log(`Example app listening on port ${port}`);
});
